package com.project.legendsofleague.domain.item.domain;


import com.project.legendsofleague.common.BaseEntity;
import com.project.legendsofleague.domain.item.dto.ItemRequestDto;
import com.project.legendsofleague.domain.purchase.exception.NotEnoughStockException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
    private String description;
    @Enumerated(EnumType.STRING)
    private ItemCategory category;
    private String thumbnailImage;
    private boolean isDeleted = false;

    @OneToMany(mappedBy = "item")
    private List<ItemImage> itemImageList = new ArrayList<>();

    public Item(Long id) {
        this.id = id;
    }



    public static Item toEntity(ItemRequestDto itemRequestDto, String thumbnailImage) {
        Item item = new Item();
        item.name = itemRequestDto.getName();
        item.price = itemRequestDto.getPrice();
        item.stock = itemRequestDto.getStock();
        item.description = itemRequestDto.getDescription();
        item.category = ItemCategory.valueOf(itemRequestDto.getCategory());
        item.thumbnailImage = thumbnailImage;

        return item;
    }

    public void removeStock(Integer count) {
        if(this.stock < count) {
            throw new NotEnoughStockException();
        }
        this.stock -= count;
    }

    public void addStock(Integer count) {
        this.stock += count;
    }
}
